import edu.riple.annotationinjector.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BasicTest {

    Injector injector;
    String srcPath = "/Desktop/Super.java";
    String fixPath = "/Desktop/fixes.java";

    @Before
    public void setup(){

        injector = Injector.builder()
                .addPath(srcPath)
                .setFixesJsonFilePath(fixPath)
                .build();
    }

    @Test
    public void basicTest(){
        System.out.println(injector.start());
    }
}
