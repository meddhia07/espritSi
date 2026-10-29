package tn.esprit.nomprenomclasse3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class NomPrenomClasse3ApplicationTests {

    @Test
    void contextLoads(@Autowired ApplicationContext context) {
        assertEquals(9, context.getBeanNamesForType(Repository.class).length);
    }

}
