package ru.pets.batch.batch;

import org.springframework.jdbc.core.RowMapper;
import ru.pets.batch.Pet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PetRowMapper implements RowMapper<Pet> {

    @Override
    public Pet mapRow(ResultSet resultSet, int i) throws SQLException {
        Pet pet = new Pet();
        pet.setId(resultSet.getLong("id"));
        pet.setName(resultSet.getString("name"));
        pet.setAge(resultSet.getInt("age"));
        pet.setWeight(resultSet.getDouble("weight"));
        return pet;
    }
}
