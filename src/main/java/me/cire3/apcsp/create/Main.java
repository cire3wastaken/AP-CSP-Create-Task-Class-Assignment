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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
    }

    public static ImageData loadImageFile(InputStream data, String mime) {
        try {
            BufferedImage img = ImageIO.read(data);
            if(img == null) {
                throw new IOException("Data is not a supported image format!");
            }
            int w = img.getWidth();
            int h = img.getHeight();
            boolean a = img.getColorModel().hasAlpha();
            int[] pixels = new int[w * h];
            img.getRGB(0, 0, w, h, pixels, 0, w);
            for (int i = 0; i < pixels.length; ++i) {
                int j = pixels[i];
                if (!a) {
                    j = j | 0xFF000000;
                }
                pixels[i] = (j & 0xFF00FF00) | ((j & 0x00FF0000) >>> 16) | ((j & 0x000000FF) << 16);
            }
            return new ImageData(w, h, pixels, a);
        }catch(IOException ex) {
            return null;
        }
    }

    private static byte[] convert(int[] ints) {
        byte[] out = new byte[ints.length * 4];
        for (int i = 0; i < ints.length; i++) {
            int pixel = ints[i];
            int base  = i * 4;

            out[base    ] = (byte) ((pixel      ) & 0xFF);
            out[base + 1] = (byte) ((pixel >>  8) & 0xFF);
            out[base + 2] = (byte) ((pixel >> 16) & 0xFF);
            out[base + 3] = (byte) ((pixel >> 24) & 0xFF);
        }
        return out;
    }

    private record ImageData(int w, int h, int[] pixels, boolean a) {}

    public static void saveRgba2Png(byte[] rgbaBytes, int width, int height, File output) throws IOException {
        if (rgbaBytes.length != width * height * 4) {
            throw new IllegalArgumentException("Byte array size does not match width * height * 4");
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 4;

                int r = rgbaBytes[index] & 0xFF;
                int g = rgbaBytes[index + 1] & 0xFF;
                int b = rgbaBytes[index + 2] & 0xFF;
                int a = rgbaBytes[index + 3] & 0xFF;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
            }
        }

        if (output.getParentFile() != null)
            output.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", output);
    }
}
