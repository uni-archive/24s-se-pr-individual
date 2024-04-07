import {Component, OnInit} from '@angular/core';
import {TournamentDetailDto, TournamentStandingsDto} from "../../../dto/tournament";
import {TournamentService} from "../../../service/tournament.service";
import {ActivatedRoute} from "@angular/router";
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
            // TODO show an error message to the user. Include and sensibly present the info from the backend!
          }
        });

        this.service.getStandingsById(id).subscribe({
          next: standings => {
            this.standings = standings;
            console.log(standings)
          },
          error: error => {
            console.error('Error loading standings', error);
            // TODO show an error message to the user. Include and sensibly present the info from the backend!
          }
        });
      }
    });
  }

  public submit(form: NgForm) {
    //todo output
    console.log(this.standings)
    this.service.updateStandingsTreeById(this.tournament!.id, this.standings!.tree).subscribe({
    error: error => {
      console.error('Error loading standings', error);
      // TODO show an error message to the user. Include and sensibly present the info from the backend!
    }
  });
  }

  public generateFirstRound() {
    if (!this.standings)
      return;
    // TODO implement
    console.log("test")
    this.service.generateFirstRounds(this.tournament!.id, this.standings!.tree).subscribe({
      next: tree => {
        this.standings!.tree = tree;
        console.log(tree)
      },
      error: error => {
        console.error('Error loading standings', error);
        // TODO show an error message to the user. Include and sensibly present the info from the backend!
      }
    });
  }
}
