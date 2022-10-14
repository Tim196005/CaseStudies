package io.casestudy.price.pricingrules;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import io.casestudy.price.cache.PriceCacheService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PricingServiceImpl implements PricingService {

	private final PriceCacheService priceCacheService;

	private final Random rand;

	private final DateTimeFormatter formatter;

	private AtomicInteger cacheLength;

	public PricingServiceImpl(PriceCacheService priceCacheService) {
		this.priceCacheService = priceCacheService;
		this.rand = new Random();
		this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		this.cacheLength = new AtomicInteger();
	}

	/*
	 * 1..100 flights will generate ~0.6Gb of data, so it can be run 5 times to generate ~3Gb
	 */
	@Override
	public void warmupCache() {
		Collection<LocalDate> dateRange = dateRange(LocalDate.now(), LocalDate.now().plusYears(2)).get();
		log.info("Warmup pricing cache for {} days", dateRange.size());
		IntStream.range(1, 500).forEach(i -> {
			dateRange.forEach(date -> {
				int factor = range(1000, 10000);
				String flight = "EK".concat(String.format("%04d", i));
				log.info("Flight {}", flight);
				Price price = createPrice(flight, formatter.format(date), price(factor),
						new byte[8192]);
				priceCacheService.put().apply(price.cacheKey(), price);
				cacheLength.getAndIncrement();
			});
		});
		log.info("Cache warmed with {} prices", cacheLength);
	}

	Supplier<Collection<LocalDate>> dateRange(LocalDate start, LocalDate end) {
		return () -> {
			List<LocalDate> range = new ArrayList<LocalDate>();
			LocalDate tmp = start;
			while (tmp.isBefore(end) || tmp.equals(end)) {
				range.add(tmp);
				tmp = tmp.plusDays(1);
			}
			return range;
		};
	}

	int range(int low, int high) {
		return rand.nextInt(high - low) + low;
	}

	double price(int factor) {
		return rand.nextDouble() * factor;
	}

	Price createPrice(String flight, String date, Double price, byte[] contents) {
		Arrays.fill(contents, (byte) 0xa);
		return new Price(flight, date, price, contents);
	}

}
