package com.relatosburger.api.status;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatusController {

	@GetMapping("/status")
	public Map<String, Object> status() {
		return Map.of(
				"status", "ok",
				"app", "relatos-burger-api");
	}

}
