package test;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import java.lang.reflect.Method;
public class Test {
    public static void main(String[] args) {
        System.out.println("Screen methods:");
        for(Method m : Screen.class.getDeclaredMethods()) {
            if(m.getName().toLowerCase().contains("shift")) {
                System.out.println(m);
            }
        }
        System.out.println("Window methods:");
        for(Method m : Window.class.getDeclaredMethods()) {
            if(m.getReturnType() == long.class) {
                System.out.println(m);
            }
        }
    }
}
