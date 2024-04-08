import {Component, OnInit} from '@angular/core';
import {TournamentDetailDto, TournamentStandingsDto, TournamentStandingsTreeDto} from "../../../dto/tournament";
import {TournamentService} from "../../../service/tournament.service";
import {ActivatedRoute, Router} from "@angular/router";
import {NgForm} from "@angular/forms";
import {Location} from "@angular/common";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../service/error-formatter.service";

@Component({
  selector: 'app-tournament-standings',
  templateUrl: './tournament-standings.component.html',
  styleUrls: ['./tournament-standings.component.scss']
})
export class TournamentStandingsComponent implements OnInit {
  standings: TournamentStandingsDto | undefined;
  tournament: TournamentDetailDto | undefined;

  public constructor(
    private service: TournamentService,
    private errorFormatter: ErrorFormatterService,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private location: Location,
    private router: Router
  ) {
  }

  public ngOnInit() {
    this.route.params.subscribe(params => {
      const id = Number(params.id);
      if (id) {
        this.service.getById(id).subscribe({
          next: tournament => {
            this.tournament = tournament;
          },
          error: error => {
            console.error('Error loading tournament', error);
            const errorMessage = (() => {
              switch (error.status) {
                case 404: return "Tournament with ID " + id + " does not exist.";
                case 0: return "Is the backend up?"
                default: return 'Error Loading tournament: ' + error.message
              }
            })();
            this.router.navigate(["/tournaments"])
            this.notification.error(errorMessage, 'Could not load tournament');
          }
        });

        this.service.getStandingsById(id).subscribe({
          next: standings => {
            this.standings = standings;
            console.log(standings)
          },
          error: error => {
            console.error('Error loading standings', error);
            // dont show an extra error in the notifications
          }
        });
      }
    });
  }

  public submit(form: NgForm) {
    this.service.updateStandingsTreeById(this.tournament!.id, this.standings!.tree).subscribe({
    error: error => {
      console.error('Error updating standings', error);
      const errorMessage = (() => {
        switch (error.status) {
          case 422: return this.errorFormatter.format(error)
          case 0: return "Is the backend up?"
          default: return 'Error Loading Tournament: ' + error.message
        }
      })();
      this.notification.error(errorMessage, 'Could not update standings', {
        enableHtml: true,
        timeOut: 10000,
      });
    }
  });
  }

  public generateFirstRound() {
    if (!this.standings)
      return;

    const treeIsEmpty: any = (b: TournamentStandingsTreeDto) => {
      if (b.thisParticipant !== null) return false;
      if (b.branches == null || b.branches.length === 0) return true;
      return treeIsEmpty(b.branches[0]) && treeIsEmpty(b.branches[1]);
    }
    if (! treeIsEmpty(this.standings.tree)) {
      this.notification.warning("Standings tree must be empty", "Cannot generate first round")
      return;
    }

    this.service.generateFirstRounds(this.tournament!.id, this.standings!.tree).subscribe({
      next: tree => {
        this.standings!.tree = tree;
      },
      error: error => {
        console.error('Error generating standings', error);
        const errorMessage = (() => {
          switch (error.status) {
            case 422: return this.errorFormatter.format(error)
            case 0: return "Is the backend up?"
            default: return 'Error generating standings: ' + error.message
          }
        })();
        this.notification.error(errorMessage, 'Could not generate standings', {
          enableHtml: true,
          timeOut: 10000,
        });
      }
    });
  }
}
