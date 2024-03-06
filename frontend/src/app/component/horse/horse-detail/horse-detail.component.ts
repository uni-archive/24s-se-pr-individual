import {Component, OnInit} from '@angular/core';
import {Horse} from "../../../dto/horse";
import {Sex} from "../../../dto/sex";
import {HorseService} from "../../../service/horse.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrl: './horse-detail.component.scss'
})
export class HorseDetailComponent implements OnInit {
  horse: Horse = {
    name: '',
    sex: Sex.female,
    dateOfBirth: new Date(), // TODO this is bad
    height: 0, // TODO this is bad
    weight: 0, // TODO this is bad
  };

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService,
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = Number(params.id);
      if (id) {
        this.service.getById(id).subscribe({
          next: horse => {
            this.horse = horse;
          },
          error: error => {
            console.error('Error loading horse', error);
            // TODO show an error message to the user. Include and sensibly present the info from the backend!
          }
        });
      }
    });
  }


  public onDelete() {
    this.service.delete(this.horse.id!).subscribe({
      next: data => {
        this.notification.success(`Horse ${this.horse.name} successfully deleted.`);
        this.router.navigate(['/horses']);
      },
      error: error => {
        console.error('Error deleting horse', error);
        // TODO show an error message to the user. Include and sensibly present the info from the backend!
      }
    });
  }
}
