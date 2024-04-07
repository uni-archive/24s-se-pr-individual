package at.ac.tuwien.sepr.assignment.individual.entity;

/**
 * Represents a horse in the persistent data store.
 */
public class TournamentTree {
  private Long id;
  private Long tournamentId;
  private Long participantId;
  private Long parentId;
  private BranchPosition branchPosition;
  private int firstRoundIndex;

  public int getFirstRoundIndex() {
    return firstRoundIndex;
  }

  public TournamentTree setFirstRoundIndex(int firstRoundIndex) {
    this.firstRoundIndex = firstRoundIndex;
    return this;
  }

  public Long getId() {
    return id;
  }

  public TournamentTree setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getTournamentId() {
    return tournamentId;
  }

  public TournamentTree setTournamentId(Long tournamentId) {
    this.tournamentId = tournamentId;
    return this;
  }

  public Long getParticipantId() {
    return participantId;
  }

  public TournamentTree setParticipantId(Long participantId) {
    this.participantId = participantId;
    return this;
  }

  public Long getParentId() {
    return parentId;
  }

  public TournamentTree setParentId(Long parentId) {
    this.parentId = parentId;
    return this;
  }

  public BranchPosition getBranchPosition() {
    return branchPosition;
  }

  public TournamentTree setBranchPosition(BranchPosition branchPosition) {
    this.branchPosition = branchPosition;
    return this;
  }

  @Override
  public String toString() {
    return "TournamentTree{" +
        "id=" + id +
        ", tournamentId=" + tournamentId +
        ", participantId=" + participantId +
        ", parentId=" + parentId +
        ", branchPosition=" + branchPosition +
        '}';
  }
}
