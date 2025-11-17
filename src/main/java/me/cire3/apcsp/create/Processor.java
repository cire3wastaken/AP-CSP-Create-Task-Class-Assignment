/*
 * Copyright (c) 2025 cire3. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package me.cire3.apcsp.create;

import me.cire3.apcsp.create.noise.WeightedGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static me.cire3.apcsp.create.MathUtils.clamp;

public class Processor {
    private final File inputFile;
    private final List<WeightedGenerator> generators;

    public Processor(File inputFile, List<WeightedGenerator> generators) {
        this.inputFile = inputFile;
        this.generators = generators;
    }

    public void run() {
        try {
            BufferedImage image = ImageIO.read(inputFile);

            float[][] noise = new float[image.getWidth()][image.getHeight()];

            for (int x = 0; x < image.getWidth(); x++) {
                noise[x] = new float[image.getHeight()];
                Arrays.fill(noise[x], 1.0f);
            }

            for (WeightedGenerator generator : generators) {
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        noise[x][y] += generator.samplePoint(x, y);
                    }
                }
            }

            BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    output.setRGB(x, y, applyNoise(noise[x][y], image.getRGB(x, y)));
                }
            }

            ImageIO.write(output, "PNG", new File("testing.png"));

            System.out.println("Output: " + new File("testing.png").getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error reading file: " + inputFile.getAbsolutePath());
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private static int applyNoise(float noise, int rgba) {
        int a = (rgba >>> 24) & 0xFF;
        int r = (rgba >>> 16) & 0xFF;
        int g = (rgba >>> 8)  & 0xFF;
        int b =  rgba         & 0xFF;

        float mul = 1f + noise * 0.10f;
        float add = noise * 20f;

        r = (int) clamp((int) (r * mul + add), 0, 255);
        g = (int) clamp((int) (g * mul + add), 0, 255);
        b = (int) clamp((int) (b * mul + add), 0, 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
