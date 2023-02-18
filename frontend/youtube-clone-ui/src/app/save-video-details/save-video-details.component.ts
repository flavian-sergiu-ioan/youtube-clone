import { Component } from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {MatChipInputEvent} from '@angular/material/chips';
import {COMMA, ENTER} from '@angular/cdk/keycodes';


@Component({
  selector: 'app-save-video-details',
  templateUrl: './save-video-details.component.html',
  styleUrls: ['./save-video-details.component.css']
})
export class SaveVideoDetailsComponent {

     saveVideoDetailsForm: FormGroup;
     title: FormControl = new FormControl('');
     description: FormControl = new FormControl('');
     videoStatus: FormControl = new FormControl('');

     addOnBlur = true;
     readonly separatorKeysCodes = [ENTER, COMMA] as const;
     tags: string[] = [];

    constructor() {
        this.saveVideoDetailsForm = new FormGroup({
        title: this.title,
        description: this.description,
        videoStatus: this.videoStatus })
    }

    add(event: MatChipInputEvent): void {
      const tag = (event.value || '').trim();
      // Add our fruit
      if (tag) {
        this.tags.push(tag);
      }
      // Clear the input value
      event.chipInput!.clear();
    }

    remove(tag: string): void {
      const index = this.tags.indexOf(tag);
      if (index >= 0) {
        this.tags.splice(index, 1);
      }
    }
}
