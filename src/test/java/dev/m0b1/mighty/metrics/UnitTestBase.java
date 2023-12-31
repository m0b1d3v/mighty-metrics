package dev.m0b1.mighty.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class UnitTestBase {

  /**
   * This is purely a function for coverage percentage vanity, do not put any stock in it.
   *
   * @see <a href="https://stackoverflow.com/q/64896125">StackOverflow</a>
   */
  protected void assertUtilityClass(Class<?> utilityClass) throws NoSuchMethodException {

    var constructor = utilityClass.getDeclaredConstructor();
    constructor.setAccessible(true);

    var e1 = assertThrows(InvocationTargetException.class, constructor::newInstance);
    var e2 = e1.getTargetException();

    assertEquals(UnsupportedOperationException.class, e2.getClass());
    assertEquals("This is a utility class and cannot be instantiated", e2.getMessage());
  }

}
