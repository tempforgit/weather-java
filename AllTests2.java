package io.weatherTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.github.ideaqe.weather.WeatherServer;

@RunWith(Suite.class)
@SuiteClasses({ UnitTest2.class, UnitTestAllTemps.class })

public class AllTests2 {
	static WeatherServer server;
	@BeforeClass
	public static void init() throws Exception {
		
        server = new WeatherServer(8080);
        server.start(); 
	}

	@AfterClass
	public static void cleanup() throws Exception {
        server.stop();
	}
	
}
