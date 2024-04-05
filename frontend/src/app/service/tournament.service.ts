import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {Observable, tap, throwError} from 'rxjs';
import {formatIsoDate} from '../util/date-helper';
import {
  TournamentCreateDto, TournamentDetailDto,
  TournamentListDto,
  TournamentSearchParams, TournamentStandingsDto,
} from "../dto/tournament";
import {Horse} from "../dto/horse";
const baseUri = environment.backendUrl + '/tournaments';

class ErrorDto {
  constructor(public message: String) {}
}

@Injectable({
  providedIn: 'root'
})
export class TournamentService {
  constructor(
    private http: HttpClient
  ) {
  }


  public create(tournament: TournamentCreateDto): Observable<TournamentDetailDto> {
    return this.http.post<TournamentDetailDto>(
      baseUri,
      tournament
    );
  }

  /**
   * Get a list of all tournaments in the system.
   * @param searchParams the search parameters to filter the list of tournaments
   * @return an Observable for the list of tournaments
   */
  public search(searchParams: TournamentSearchParams): Observable<TournamentListDto[]> {
    if (searchParams.name === '') {
      delete searchParams.name;
    }
    let params = new HttpParams();
    if (searchParams.name) {
      params = params.append('name', searchParams.name);
    }
    if (searchParams.startDate) {
      params = params.append('startDate', formatIsoDate(searchParams.startDate));
    }
    if (searchParams.endDate) {
      params = params.append('endDate', formatIsoDate(searchParams.endDate));
    }
    if (searchParams.limit) { // todo implement limit in search page
      params = params.append('limit', searchParams.limit);
    }
    return this.http.get<TournamentListDto[]>(baseUri, { params })
      .pipe(tap(tournaments => tournaments.map(t => {
        t.startDate = new Date(t.startDate); // Parse date string
        t.endDate = new Date(t.endDate); // Parse date string
      })));
  }

  /**
   * Get a single tournament by its id.
   * @param id the id of the tournament to get
   * @return an Observable for the tournament
   */
  public getById(id: number): Observable<TournamentDetailDto> {
    return this.http.get<TournamentDetailDto>(`${baseUri}/${id}`);
  }

  /**
   * Get a single tournament by its id.
   * @param id the id of the tournament to get
   * @return an Observable for the tournament
   */
  public getStandingsById(id: number): Observable<TournamentStandingsDto> {
    return this.http.get<TournamentStandingsDto>(`${baseUri}/${id}/standings`);
  }

}
