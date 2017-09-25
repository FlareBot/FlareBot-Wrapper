package stream.flarebot.flarebotwrapper;

import spark.Route;

public class Routes {

    public static Route getUpdateRoute() {
        return (request, response) -> {
             response.body("Updating!");
             response.status(200);
             FlareBotWrapper.update();
             return "";
        };
    }

}
