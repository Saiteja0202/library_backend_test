package com.cts.library.service;

public interface FineService {

	void processDailyFines();

	void payFine(Long fineId);
}
