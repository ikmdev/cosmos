package dev.ikm.server.rest.boundary;

import dev.ikm.server.database.controller.Find;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/pattern")
public class PatternResource {

	private final Find find = new Find();

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWithId(
			@PathParam("id") int nid,
			@QueryParam("language") UUID languageCoordinate) {
		return Response
				.ok(find.patternChronology(nid, languageCoordinate))
				.build();
	}

	@GET
	@Path("/publicid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWithPublicId(
			@PathParam("uuid") UUID uuid,
			@QueryParam("language") UUID languageCoordinate) {
		return Response
				.ok(find.patternChronology(uuid, languageCoordinate))
				.build();
	}

}
