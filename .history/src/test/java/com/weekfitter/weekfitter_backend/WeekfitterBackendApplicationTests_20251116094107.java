package com.weekfitter.weekfitter_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Jednoduchý integrační test ověřující, že se Spring Boot kontext aplikace
 * dokáže úspěšně načíst bez chyb.
 *
 * Tento typ testu se označuje jako „smoke test“ a slouží k základní kontrole
 * správné konfigurace projektu, beanů, závislostí a Spring prostředí.
 *
 * Pokud by v aplikaci chyběl povinný bean, špatně fungovala konfigurace
 * nebo vznikla kolize v závislostech, tento test by selhal.
 */
@SpringBootTest
class WeekfitterBackendApplicationTests {

	
    /**
     * Testovací metoda bez obsahu – samotná anotace @SpringBootTest
     * způsobí načtení celého application contextu.
     *
     * Test projde, pokud Spring kontext naběhne bez výjimky.
     */
	@Test
	void contextLoads() {
	    // Záměrně prázdné – úspěch testu potvrzuje pouze absence výjimky
	}

}
