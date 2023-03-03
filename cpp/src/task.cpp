#include <string>
#include <iostream>
#include <fstream>

int main(int argc, char** argv) 
{
    std::ifstream ifs(argv[1]);
    std::string input;
    ifs >> input;
    std::cout << input << std::endl;
}
