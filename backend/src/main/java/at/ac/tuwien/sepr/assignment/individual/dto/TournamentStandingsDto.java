package at.ac.tuwien.sepr.assignment.individual.dto;

import java.util.List;

public record TournamentStandingsDto(
    int id,
    String name,
    List<TournamentDetailParticipantDto> participants,
    TournamentStandingsDto tree
) {
}
