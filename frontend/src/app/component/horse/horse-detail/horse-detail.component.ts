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
    dateOfBirth: new Date(),
    height: 0,
    weight: 0,
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
            const errorMessage = (() => {
              switch (error.status) {
                case 404: return "Horse with id " + id + " does not exist";
                default: return 'Error loading Horse: ' + error.message
              }
            })();
            this.router.navigate(["/horses"])
            this.notification.error(errorMessage, 'Could not load horse');
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
        const errorMessage = (() => {
          switch (error.status) {
            case 409: return "Horse is part of tournament";
            default: return 'Error Deleting Horse: ' + error.message
          }
        })();
        this.notification.error(errorMessage, 'Could not delete horse');
      }
    });
  }
}
