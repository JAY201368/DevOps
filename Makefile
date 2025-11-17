all: run

# Run both frontend and backend services concurrently
run:
	@echo "Starting Frontend and Backend services..."
	@make -j2 frontend backend

# Start the frontend development server
frontend:
	@echo "Starting Frontend..."
	cd TomatoMall_Frontend && npm run dev

# Start the backend Spring Boot application
backend:
	@echo "Starting Backend..."
	nohup ngrok http --url=stinkbug-perfect-lynx.ngrok-free.app 8080 > /dev/null 2>&1 &
	@echo "Waiting for ngrok to start..."
	cd TomatoMall_Backend && mvn -B -q spring-boot:run

# Stop both services (using process name matching)
stop:
	@echo "Stopping services..."
	-pkill -f "node.*vite"
	-pkill -f "java.*TomatoMallApplication"

clean:
	@echo "Cleaning up..."
	@cd TomatoMall_Backend && mvn clean
	@echo "Cleanup complete."

.PHONY: all run frontend backend stop
