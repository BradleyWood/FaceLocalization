package localization;

import localization.processor.DefaultFaceLocalizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A simple Test app
 */
public class App 
{
    public static void main(String[] args) throws IOException {
        if(args != null && args.length > 0) {
            for(String f : args) {
                String save = f.substring(0,f.indexOf(".")) + "_faces.jpg";
                File file = new File(f);
                BufferedImage image = ImageIO.read(file);
                DefaultFaceLocalizer dfl = new DefaultFaceLocalizer();
                List<Rectangle> rects = dfl.localize(image);
                save(image, rects, save);
            }
        } else {
            System.err.println("No args.");
        }
    }
    public static void save(BufferedImage image, List<Rectangle> lst, String file) throws IOException {
        Graphics g = image.getGraphics();
        g.setColor(Color.RED);
        System.out.println("Rects: "+lst.size());
        for(Rectangle r : lst){
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        ImageIO.write(image, "jpg", new File(file));
    }
}
