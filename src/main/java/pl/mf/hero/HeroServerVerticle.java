package pl.mf.hero;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import pl.mf.hero.dto.HeroDTO;

public class HeroServerVerticle extends AbstractVerticle {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 8090;

	private static final String PATH_LIST_HEROES = "/api/heroes";

	 private static final String BAD_REQUEST = new JsonObject().put("response", "BAD_REQUEST").toString();

	private List<HeroDTO> heroes;

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(HeroServerVerticle.class.getName());
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		initializeHeroes();

		Router router = Router.router(vertx);

		// Avoid CORS problems
		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("x-requested-with");
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");
		allowedHeaders.add("X-PINGARUNER");

		Set<HttpMethod> allowedMethods = new HashSet<>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.OPTIONS);

		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

		router.get(PATH_LIST_HEROES).handler(reqHandler -> {
			HttpServerResponse response = reqHandler.response();
			response.setChunked(true);
			response.putHeader("Content-Type", "application/json");

			if (reqHandler.queryParam("id") != null && reqHandler.queryParam("id").size() > 0) {
				if (reqHandler.queryParam("id").size() == 1 && !reqHandler.queryParam("id").get(0).isEmpty()) {
					int heroId = Integer.parseInt(reqHandler.queryParam("id").get(0));
					HeroDTO hero = heroes.stream().filter(heroDTO -> heroId == heroDTO.getId()).findFirst().orElse(null);
					response.write(hero != null ? JsonObject.mapFrom(hero).toString() : BAD_REQUEST);
				} else {
					response.write(BAD_REQUEST);
				}
			} else {
				JsonArray heroesArray = new JsonArray(heroes);
				response.write(heroesArray.toString());
			}
			reqHandler.response().end();

			System.out.format("Request -> %s\nParams -> %s\n", PATH_LIST_HEROES, reqHandler.queryParams().toString().replace("\n", " "));
		});

		vertx.createHttpServer().requestHandler(router::accept).listen(SERVER_PORT, SERVER_HOST);

		System.out.println("Server started");
		
//		new JsonObject().put("id", 1).put("name", "Hero_1").put("birthday", new Date().getTime()).mapTo(HeroDTO.class);
	}

	private void initializeHeroes() {
		heroes = new ArrayList<>();
		heroes.add(new HeroDTO(1, "Hero 1",
				Date.from(LocalDate.of(1988, Month.FEBRUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(2, "Hero 2",
				Date.from(LocalDate.of(1982, Month.JULY, 21).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(3, "Hero 3",
				Date.from(LocalDate.of(1991, Month.APRIL, 29).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(4, "Hero 4",
				Date.from(LocalDate.of(1986, Month.JUNE, 6).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(5, "Hero 5",
				Date.from(LocalDate.of(1976, Month.DECEMBER, 5).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(6, "Hero 6",
				Date.from(LocalDate.of(1986, Month.SEPTEMBER, 24).atStartOfDay(ZoneId.systemDefault()).toInstant())));
		heroes.add(new HeroDTO(7, "Hero 7",
				Date.from(LocalDate.of(1981, Month.FEBRUARY, 7).atStartOfDay(ZoneId.systemDefault()).toInstant())));
	}
}
