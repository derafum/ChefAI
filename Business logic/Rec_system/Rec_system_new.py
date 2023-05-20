import sys

test = None

if len(sys.argv) > 1:
    number = int(sys.argv[1])
    # Делайте что-то с переданным значением (например, присваивание его переменной test)
    test = number * 2

def factorial(a):
    fact = 1
    for i in range(1, a+1):
        fact *= i
    return fact

print(test)