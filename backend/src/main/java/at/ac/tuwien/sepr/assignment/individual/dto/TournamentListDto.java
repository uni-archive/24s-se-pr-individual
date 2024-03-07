package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO class for list of tournaments in search view.
 */
public record TournamentListDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate
) {
}
