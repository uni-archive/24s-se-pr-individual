package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.TournamentCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.TournamentStandingsDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with tournaments.
 */
public interface TournamentService {
  /**
   * Search for tournaments in the persistent data store matching all provided fields.
   * The name is considered a match, if the search string is a substring of the field in tournament.
   *
   * @param searchParameters the search parameters to use in filtering.
   * @return the tournaments where the given fields match.
   */
  Stream<TournamentListDto> search(TournamentSearchDto searchParameters);

  /**
   * Create a new tournament in the persistent data store.
   *
   * @param toCreate the tournament to create.
   * @return the created tournament.
   * @throws ValidationException if the tournament data is invalid.
   * @throws ConflictException if the tournament already exists.
   */
  TournamentDetailDto create(TournamentCreateDto toCreate) throws ValidationException, ConflictException;

  /**
   * Get the standings of a tournament from the persistent data store.
   *
   * @param id the id of the tournament to get.
   * @return the standings of the tournament.
   * @throws NotFoundException if the tournament does not exist.
   */
  TournamentStandingsDto getStandingsById(long id) throws NotFoundException;

  /**
   * Get the details of a tournament from the persistent data store.
   *
   * @param id the id of the tournament to get.
   * @return the details of the tournament.
   * @throws NotFoundException if the tournament does not exist.
   */
  TournamentDetailDto getById(long id) throws NotFoundException;
}
