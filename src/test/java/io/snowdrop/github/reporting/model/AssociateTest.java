package io.snowdrop.github.reporting.model;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.snowdrop.security.model.BotUser;
import io.snowdrop.security.model.BotUserRepository;

//@QuarkusTest
class AssociateTest {

  @InjectMock
  BotUserRepository repoBotUser;

//  @Test
  public void testPanacheRepositoryMocking() throws Throwable {
    // Mocked classes always return a default value
    Assertions.assertEquals(0, repoBotUser.count());

    // Now let's specify the return value
    Mockito.when(repoBotUser.count()).thenReturn(23l);
    Assertions.assertEquals(23, repoBotUser.count());

    // Now let's change the return value
    Mockito.when(repoBotUser.count()).thenReturn(42l);
    Assertions.assertEquals(42, repoBotUser.count());

    // Now let's call the original method
    Mockito.when(repoBotUser.count()).thenCallRealMethod();
    Assertions.assertEquals(0, repoBotUser.count());

    // Check that we called it 4 times
    Mockito.verify(repoBotUser, Mockito.times(4)).count();

    // Mock only with specific parameters
    BotUser p = new BotUser();
    Mockito.when(repoBotUser.findById(12l)).thenReturn(p);
    Assertions.assertSame(p, repoBotUser.findById(12l));
    Assertions.assertNull(repoBotUser.findById(42l));

    // Mock throwing
    Mockito.when(repoBotUser.findById(12l)).thenThrow(new WebApplicationException());
    Assertions.assertThrows(WebApplicationException.class, () -> repoBotUser.findById(12l));

//    Mockito.when(repoBotUser.findOrdered()).thenReturn(Collections.emptyList());
    //    Assertions.assertTrue(repoBotUser.findOrdered().isEmpty());
    //
    //    // We can even mock your custom methods
    //    Mockito.verify(repoBotUser).findOrdered();
    Mockito.verify(repoBotUser, Mockito.atLeastOnce()).findById(Mockito.any());
    Mockito.verifyNoMoreInteractions(repoBotUser);
  }
}

