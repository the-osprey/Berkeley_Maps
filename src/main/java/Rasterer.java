import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private static final double ROOT_ULLAT = MapServer.ROOT_ULLAT;
    private static final double ROOT_ULLON = MapServer.ROOT_ULLON;
    private static final double ROOT_LRLAT = MapServer.ROOT_LRLAT;
    private static final double ROOT_LRLON = MapServer.ROOT_LRLON;
    private static final double TILE_SIZE = MapServer.TILE_SIZE;
    private static final double MAP_LON_LEN = ROOT_LRLON - ROOT_ULLON;
    private static final double MAP_LAT_LEN = ROOT_ULLAT - ROOT_LRLAT;
    private static final double D0_DPP = lonDDP(ROOT_LRLON, ROOT_ULLON, TILE_SIZE) / 2;
//    private boolean query_success = false;
    //Divide by 2 because the 0 level is a comp of 4 images
//    private static final double[]  DEPTH_TILES_ACROSS= new double[]{1, 2, 3, 7, 15, 31, 63, 127};

    public Rasterer() {
        // YOUR CODE HERE
    }

    static double lonDDP(double lrlon, double ullon, double width) {
        // return longitudinal distance per pixel
        return (lrlon - ullon) / width;
    }

    static double getDLevel(double queryLrlon, double queryUllon, double queryWidth) {
        // Determine which depth file must have to satisfy resolution requirements
        double boxLonDPP = lonDDP(queryLrlon, queryUllon, queryWidth);
        double d = Math.ceil(Math.log(D0_DPP / boxLonDPP) * (1 / Math.log(2)) + 1);
        if (d >= 7) {
            return 7;
        }
        return d;
    }

    static double numTilesAcross(double depth) {
        return Math.pow(2, depth) - 1;
    }

    static double[] getImgULLon(double queryLon, double depth) {
        // returns double array with 0 being x index of TOP LEFT image
        // 1 being ULLON of TOP LEFT image
        double[] results = new double[2];
        if (queryLon < ROOT_ULLON) {
            queryLon = ROOT_ULLON;
        }
        double qDistanceFromLeft = Math.abs(ROOT_ULLON - queryLon);
        double imageTileSize = MAP_LON_LEN / (numTilesAcross(depth) + 1);
//        System.out.println((qDistanceFromLeft / imageTileSize));

        double tileNum = Math.floor(qDistanceFromLeft / imageTileSize);
        if (tileNum >= numTilesAcross(depth)) {
            tileNum = numTilesAcross(depth);

        }
        results[0] = tileNum;
        results[1] = ROOT_ULLON + imageTileSize * results[0];
        return results;
    }

    // New func for LR LON WorkS! AHH
    static double[] getImgLRLon(double queryLon, double depth) {
        // returns double array with 0 being x index of BOTTOM RIGHT image
        // 1 being LRLON of BOTTOM RIGHT image
        double[] results = new double[2];
        double qDistanceFromLeft = Math.abs(ROOT_ULLON - queryLon);
        double imageTileSize = MAP_LON_LEN / (numTilesAcross(depth) + 1);
        double tileNum = Math.floor(qDistanceFromLeft / imageTileSize);
        if (tileNum >= numTilesAcross(depth)) {
            tileNum = numTilesAcross(depth);

        }
        results[0] = tileNum;
        results[1] = ROOT_ULLON + imageTileSize * results[0] + imageTileSize;
        return results;
    }

    static double[] getImgULLat(double queryLat, double depth) {
        // returns double array with size 2, 0 is y index of TOP LEFT image
        // 1 is the ULLAT of TOP LEFT image
        double[] results = new double[2];
        if (queryLat >= ROOT_ULLAT) {
            queryLat = ROOT_ULLAT;
        }
        double qDistianceFromTop = Math.abs(ROOT_ULLAT - queryLat);
        double imageTileSize = MAP_LAT_LEN / (numTilesAcross(depth) + 1);
        double tileNum = Math.floor(qDistianceFromTop / imageTileSize);
        if (tileNum >= numTilesAcross(depth)) {
            tileNum = numTilesAcross(depth);

        }
        results[0] = tileNum;
        results[1] = ROOT_ULLAT - imageTileSize * results[0];
        return results;
    }

    static double[] getImgLRLat(double queryLat, double depth) {
        // returns double array with size 2, 0 is y index of BOTTOM RIGHT image
        // 1 is LRLAT of BOTTOM RIGHT image
        double[] results = new double[2];
        double qDistianceFromTop = Math.abs(ROOT_ULLAT - queryLat);
        double imageTileSize = MAP_LAT_LEN / (numTilesAcross(depth) + 1);
        double tileNum = Math.floor(qDistianceFromTop / imageTileSize);
        if (tileNum >= numTilesAcross(depth)) {
            tileNum = numTilesAcross(depth);

        }
        results[0] = tileNum;
        results[0] = tileNum;
        results[1] = ROOT_ULLAT - imageTileSize * results[0] - imageTileSize;
        return results;
    }

    public static void main(String[] args) {
        System.out.println(getImgULLon(-122.24163047377972, 7)[0]);
        System.out.println(getImgULLon(-122.24163047377972, 7)[1]);
        System.out.println(getImgLRLon(-122.24053369025242, 7)[0]);
        System.out.println(getImgLRLon(-122.24053369025242, 7)[1]);
        System.out.println(getImgULLat(37.87701580361881, 7)[0]); //
        System.out.println(getImgULLat(37.87701580361881, 7)[1]); //
        System.out.println(getImgLRLat(37.87548268822065, 7)[0]);
        System.out.println(getImgLRLat(37.87548268822065, 7)[1]);

        double depth = getDLevel(-122.22275132672245, -122.23995662778569, 613);
        System.out.println();
        System.out.println(depth);
        System.out.println(getImgULLon(-122.23995662778569,
                depth)[0]);
        System.out.println(getImgULLon(-122.23995662778569,
                depth)[1]);
        System.out.println(getImgLRLon(-122.22275132672245, depth)[0]);
        System.out.println(getImgLRLon(-122.22275132672245, depth)[1]);
        System.out.println(getImgULLat(37.877266154010954, depth)[0]); //
        System.out.println(getImgULLat(37.877266154010954, depth)[1]); //
        System.out.println(getImgLRLat(37.85829260830337, depth)[0]);
        System.out.println(getImgLRLat(37.85829260830337, depth)[1]);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
//        System.out.println(params);
        // example input:
        // {lrlon=-122.2104604264636,
        // ullon=-122.30410170759153, w=1085.0, h=566.0,
        // ullat=37.870213571328854, lrlat=37.8318576119893}
        Map<String, Object> results = new HashMap<>();
        boolean querySuccess = false;
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double width = params.get("w");
        double height = params.get("h");
        //return string w images who have greatest LonDPP less than or equal to LonDPP of qbox
        int depth = (int) getDLevel(lrlon, ullon, width);
        double[] imgULLon = getImgULLon(ullon, depth);
        double[] imgLRLon = getImgLRLon(lrlon, depth);
        double[] imgULLat = getImgULLat(ullat, depth);
        double[] imgLRLat = getImgLRLat(lrlat, depth);
        if (imgULLon[1] >= ROOT_ULLON || imgLRLon[1] <= ROOT_ULLON
                || imgULLat[1] <= ROOT_ULLAT || imgLRLat[1] >= ROOT_LRLAT) {
            querySuccess = true;

        }
        String[][] renderGrid = new String[Math.abs((int) imgULLat[0] - (int) imgLRLat[0]) + 1]
                [Math.abs((int) imgLRLon[0] - (int) imgULLon[0]) + 1];
        int xTile = (int) imgULLon[0];
        int yTile = (int) imgULLat[0];
        for (int i = 0; i <= Math.abs((int) imgULLat[0] - (int) imgLRLat[0]); i++) {
            for (int j = 0; j <= Math.abs((int) imgLRLon[0] - (int) imgULLon[0]); j++) {
                int d = depth;
                renderGrid[i][j] = "d" + Integer.toString(d) + "_x"
                        + Integer.toString(xTile + j) + "_y" + Integer.toString(yTile + i) + ".png";
            }
        }
        results.put("depth", depth);
        results.put("render_grid", renderGrid);
        //works
        results.put("raster_ul_lon", imgULLon[1]);
        results.put("raster_ul_lat", imgULLat[1]);
        results.put("raster_lr_lon", imgLRLon[1]);
        results.put("raster_lr_lat", imgLRLat[1]);
        results.put("query_success", querySuccess);
//        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
//                           + "your browser.");
        return results;
    }
}
