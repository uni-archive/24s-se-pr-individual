package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to bundle the query parameters used in searching horses.
 * Each field can be null, in which case this field is not filtered by.
 */
public record TournamentSearchDto(
    String name,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,
    Integer limit
) {
}
