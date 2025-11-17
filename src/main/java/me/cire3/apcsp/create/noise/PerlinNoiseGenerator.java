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

package me.cire3.apcsp.create.noise;

import static me.cire3.apcsp.create.MathUtils.clamp;

public class PerlinNoiseGenerator implements NoiseGenerator {
    private static final int[] PERMUTATIONS = new int[512];

    private static final int[] PRE_GENNED_PERMS = {
            151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
            8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117,
            35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71,
            134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41,
            55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89,
            18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226,
            250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
            189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43,
            172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
            251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
            49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
            138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    static {
        for (int i = 0; i < 256; i++) {
            PERMUTATIONS[256 + i] = PERMUTATIONS[i] = PRE_GENNED_PERMS[i];
        }
    }

    private float scale;

    public PerlinNoiseGenerator(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public float samplePoint(float x, float y) {
        int X = (int) Math.floor(x) & 0xFF;
        int Y = (int) Math.floor(y) & 0xFF;

        float dx = (float) (x - Math.floor(x));
        float dy = (float) (y - Math.floor(y));

        float u = fquint(dx);
        float v = fquint(dy);

        int A = PERMUTATIONS[X] + Y;
        int AA = PERMUTATIONS[A];
        int AB = PERMUTATIONS[A + 1];
        int B = PERMUTATIONS[X + 1] + Y;
        int BA = PERMUTATIONS[B];
        int BB = PERMUTATIONS[B + 1];

        return clamp(interpolate(
                0,
                interpolate(
                        v,
                        interpolate(
                                u,
                                grad(PERMUTATIONS[AA], x, y, 0),
                                grad(PERMUTATIONS[BA], x - 1, y, 0)
                        ),
                        interpolate(
                                u,
                                grad(PERMUTATIONS[AB], x, y - 1, 0),
                                grad(PERMUTATIONS[BB], x - 1, y - 1, 0)
                        )
                ),
                interpolate(
                        v,
                        interpolate(
                                u,
                                grad(PERMUTATIONS[AA + 1], x, y, -1),
                                grad(PERMUTATIONS[BA + 1], x - 1, y, -1)
                        ),
                        interpolate(
                                u,
                                grad(PERMUTATIONS[AB + 1], x, y - 1, -1),
                                grad(PERMUTATIONS[BB + 1], x - 1, y - 1, -1)
                        )
                )
        ) * scale, -3, 3);
    }

    private static float grad(int h, float x, float y, float z) {
        int hash = h & 0x0F;
        float u = hash < 8 ? x : y;
        float v = hash < 4 ? y : (hash == 12 || hash == 14) ? x : z;
        return ((hash & 1) == 0 ? u : -u) + ((hash & 2) == 0 ? v : -v);
    }

    private static float interpolate(float a, float b, float partial) {
        return b * partial + a * (1 - partial);
    }

    private static float fquint(float frag) {
        return frag * frag * frag * (frag * (frag * 6 - 15) + 10);
    }
}
