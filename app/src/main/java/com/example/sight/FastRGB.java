package com.example.sight;

//public static class FastRGB {
//
//    private int width;
//    private int height;
//    private boolean hasAlphaChannel;
//    private int pixelLength;
//    private int[] pixels;
//
//    public FastRGB(Bitmap img) { // image must be BufferedImage.TYPE_3BYTE_BGR
////            pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        pixels = new int[img.getWidth() * img.getHeight()];
//        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
//        hasAlphaChannel = img.hasAlpha();
//        width = img.getWidth();
//        height = img.getHeight();
////            hasAlphaChannel = image.getAlphaRaster() != null;
//        pixelLength = 3;
//        if (hasAlphaChannel) {
//            pixelLength = 4;
//        }
//    }
//
//    //RGB 合成一个 Int
//    int getRGB(int x, int y) {
//        int pos = (y * pixelLength * width) + (x * pixelLength);
//        int argb = -16777216; // 255 alpha
//        if (hasAlphaChannel) {
//            argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
//        }
//
//        argb += ((int) pixels[pos++] & 0xff); // blue
//        argb += (((int) pixels[pos++] & 0xff) << 8); // green
//        argb += (((int) pixels[pos++] & 0xff) << 16); // red
//        return argb;
//    }
//
//    /**
//     * 分开存放的
//     *
//     * @param x
//     * @param y
//     * @return RGB 数组
//     */
//    int[] getRGB2(int x, int y) {
//        int color = getRGB(x, y);
//        int red = ((color & 0xff0000) >> 16);
//        int green = ((color & 0xff00) >> 8);
//        int blue = (color & 0xff);
//        return new int[]{red, green, blue};
//    }
//
//    /**
//     * 存 高宽RGB，三维数组
//     *
//     * @return 三维数组
//     */
//    public int[][][] toRGBPixels() {
//        int[][][] pixels = new int[this.height][][];
//        for (int y = 0; y < this.height; y++) {
//            int[][] width = new int[this.width][];
//            for (int x = 0; x < this.width; x++) {
//                int[] rgb = getRGB2(x, y);
//                width[x] = rgb;
//            }
//            pixels[y] = width;
//        }
//        return pixels;
//    }
//
//}