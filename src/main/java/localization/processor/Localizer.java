package localization.processor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by Brad on 5/18/2017.
 */
public abstract class Localizer {

    public abstract void update(BufferedImage image);

    public abstract List<Rectangle> localize();

}
