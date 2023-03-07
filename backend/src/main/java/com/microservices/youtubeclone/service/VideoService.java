package com.microservices.youtubeclone.service;

import com.microservices.youtubeclone.dto.CommentDto;
import com.microservices.youtubeclone.dto.UploadVideoResponse;
import com.microservices.youtubeclone.dto.VideoDto;
import com.microservices.youtubeclone.model.Comment;
import com.microservices.youtubeclone.model.Video;
import com.microservices.youtubeclone.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final S3Service s3Service;
    private final VideoRepository videoRepository;
    private final UserService userService;

    public UploadVideoResponse uploadVideo(final MultipartFile multipartFile) {
       final String videoUrl = s3Service.uploadFile(multipartFile);
       var video = new Video();
       video.setVideoUrl(videoUrl);

       var savedVideo = videoRepository.save(video);
       return new UploadVideoResponse(savedVideo.getId(), savedVideo.getVideoUrl());

    }

    public VideoDto editVideo(VideoDto videoDto) {
        var savedVideo = getVideoById(videoDto.getId());
        savedVideo.setTitle(videoDto.getTitle());
        savedVideo.setDescription(videoDto.getDescription());
        savedVideo.setTags(videoDto.getTags());
        savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());
        savedVideo.setVideoStatus(videoDto.getVideoStatus());

        videoRepository.save(savedVideo);

        return videoDto;
    }

    public String uploadThumbnail(MultipartFile multipartFile, String videoId) {
        var savedVideo = getVideoById(videoId);
        String thumbnailUrl = s3Service.uploadFile(multipartFile);
        savedVideo.setThumbnailUrl(thumbnailUrl);
        videoRepository.save(savedVideo);
        return thumbnailUrl;
    }

    private Video getVideoById(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Can not find video by id - " + videoId));
    }

    public VideoDto getVideoDetails(String videoId) {
        var savedVideo = getVideoById(videoId);
        increseVideoCount(savedVideo);
        userService.addVideoToHistory(videoId);
        return mapToVideoDto(savedVideo);
    }

    private void increseVideoCount(Video savedVideo) {
        savedVideo.incrementVideoCount();
        videoRepository.save(savedVideo);
    }

    public VideoDto likeVideo(String videoId) {
        Video video = getVideoById(videoId);

        if(userService.ifLikedVideo(videoId)) {
            video.decrementLikes();
            userService.removeFromLikedVideos(videoId);
        } else if (userService.ifDislikedVideo(videoId)) {
            video.decrementDislikes();
            userService.removeFromDislikedVideos(videoId);
            video.incrementLikes();
            userService.addToLikeVideos(videoId);
        } else {
            video.incrementLikes();
            userService.addToLikeVideos(videoId);
        }

        videoRepository.save(video);

        return mapToVideoDto(video);
    }

    public VideoDto dislikeVideo(String videoId) {
        Video video = getVideoById(videoId);

        if(userService.ifDislikedVideo(videoId)) {
            video.decrementDislikes();
            userService.removeFromDislikedVideos(videoId);
        } else if (userService.ifLikedVideo(videoId)) {
            video.decrementLikes();
            userService.removeFromLikedVideos(videoId);
            video.incrementDislikes();
            userService.addToDislikedVideos(videoId);
        } else {
            video.incrementDislikes();
            userService.addToDislikedVideos(videoId);
        }

        videoRepository.save(video);

        return mapToVideoDto(video);
    }

    private VideoDto mapToVideoDto(Video video) {
        VideoDto videoDto = new VideoDto();
        videoDto.setVideoUrl(video.getVideoUrl());
        videoDto.setThumbnailUrl(video.getThumbnailUrl());
        videoDto.setId(video.getId());
        videoDto.setTitle(video.getTitle());
        videoDto.setDescription(video.getDescription());
        videoDto.setTags(video.getTags());
        videoDto.setVideoStatus(video.getVideoStatus());
        videoDto.setLikeCount(video.getLikes().get());
        videoDto.setDislikeCount(video.getDislikes().get());
        videoDto.setViewCount(video.getViewCount().get());
        return videoDto;
    }

    public void addComment(String videoId, CommentDto commentDto) {
        Video video = getVideoById(videoId);
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthorId(commentDto.getAuthorId());
        video.addComment(comment);

        videoRepository.save(video);
    }

    public List<CommentDto> getAllComments(String videoId) {
        Video video = getVideoById(videoId);
        List<Comment> commentList = video.getCommentList();
        return commentList.stream().map(this::mapToCommentDto).collect(Collectors.toList());
    }

    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(comment.getText());
        commentDto.setAuthorId(comment.getAuthorId());
        return commentDto;
    }

    public List<VideoDto> getAllVideos() {
        return videoRepository.findAll().stream().map(this::mapToVideoDto).collect(Collectors.toList());
    }
}
