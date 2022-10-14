package io.casestudy.price.pricingrules;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Price {

	String flight;

	String date;

	Double price;

	byte[] contents;

	String cacheKey() {
		return flight.concat("|").concat(date);
	}

	@Override
	public String toString() {
		return new JsonObject().put("flight", flight).put("date", date).put("price", price).toString();
	}

}
