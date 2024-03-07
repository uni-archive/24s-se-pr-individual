package at.ac.tuwien.sepr.assignment.individual.entity;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a tournament in the persistent data store.
 */
public class Tournament {
  private Long id;
  private String name;
  private LocalDate startDate;

  private LocalDate endDate;

  private List<Long> horseIds;

  public Long getId() {
    return id;
  }

  public Tournament setId(Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Tournament setName(String name) {
    this.name = name;
    return this;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public Tournament setStartDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public Tournament setEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  public List<Long> getHorseIds() {
    return horseIds;
  }

  public Tournament setHorseIds(List<Long> horseIds) {
    this.horseIds = horseIds;
    return this;
  }

  @Override
  public String toString() {
    return "Tournament{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", startDate=" + startDate
        + ", endDate=" + endDate
        + ", horseIds=" + horseIds
        + '}';
  }
}
