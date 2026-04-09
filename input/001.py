class Car:
    def __init__(self, brand, model, year):
        self.brand = brand
        self.model = model
        self.year = year
        print(f"Car {self.brand} {self.model} is created.")


    def display_info(self):
        print(f"Car Details: {self.year} {self.brand} {self.model}")

    def start(self):
        print(f"{self.brand} {self.model} is starting...")


    def __del__(self):
        print(f"Car {self.brand} {self.model} is being destroyed.")


car1 = Car("Toyota", "Corolla", 2022)


car1.display_info()
car1.start()


del car1
