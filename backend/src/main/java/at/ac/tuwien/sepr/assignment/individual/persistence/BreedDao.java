package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.BreedSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Breed;
import java.util.Collection;
import java.util.Set;

public interface BreedDao {
  /**
   * This method retrieves all breeds from the database.
   *
   * @return a collection of all Breed entities
   */
  Collection<Breed> allBreeds();

  /**
   * This method finds and retrieves breeds by their IDs.
   *
   * @param breedIds a set of breed IDs
   * @return a collection of Breed entities that match the given IDs
   */
  Collection<Breed> findBreedsById(Set<Long> breedIds);

  /**
   * This method searches for breeds based on the provided search parameters.
   *
   * @param searchParams the search parameters encapsulated in a BreedSearchDto object
   * @return a collection of Breed entities that match the search parameters
   */
  Collection<Breed> search(BreedSearchDto searchParams);
}
