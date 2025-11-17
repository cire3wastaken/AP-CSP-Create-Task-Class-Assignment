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

import me.cire3.apcsp.create.MathUtils;

import java.security.SecureRandom;

public class ValueNoiseGenerator implements NoiseGenerator {
    private final SecureRandom random = new SecureRandom();

    private final int[] grid;
    private final int[] permutation;
    private final int dimension;
    private final int mask;

    private float scale;

    public ValueNoiseGenerator(int width, int height, float scale) {
        this.dimension = Math.max(width, height);
        this.mask = dimension - 1;

        this.grid = new int[dimension];
        this.permutation = new int[dimension * 2];

        for (int i = 0; i < dimension; i++) {
            grid[i] = random.nextInt();
            permutation[i] = i;
        }

        for (int i = 0; i < dimension; i++) {
            int k = random.nextInt(0, Integer.MAX_VALUE) & mask;
            int tmp = permutation[k];
            permutation[k] = permutation[i];
            permutation[i] = tmp;
            permutation[k + dimension] = permutation[k];
        }

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
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);

        float dx = x - xi;
        float dy = y - yi;

        int residueX0 = xi & mask;
        int residueX1 = (residueX0 + 1) & mask;
        int residueY0 = yi & mask;
        int residueY1 = (residueY0 + 1) & mask;

        float c1 = grid[permutation[permutation[residueX0] + residueY0] & mask];
        float c2 = grid[permutation[permutation[residueX1] + residueY0] & mask];
        float c3 = grid[permutation[permutation[residueX0] + residueY1] & mask];
        float c4 = grid[permutation[permutation[residueX1] + residueY1] & mask];

        float sx = ss(dx);
        float sy = ss(dy);

        float ix0 = interpolate(c1, c2, sx);
        float iy0 = interpolate(c3, c4, sy);

        return MathUtils.clamp(interpolate(ix0, iy0, sy), -2, 2);
    }

    private static float ss(float frag) {
        return frag * frag * (3 - 2 * frag);
    }

    private static float interpolate(float a, float b, float partial) {
        return b * partial + a * (1 - partial);
    }
}
