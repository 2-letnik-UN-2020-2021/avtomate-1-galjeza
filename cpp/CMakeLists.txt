cmake_minimum_required(VERSION 3.13)
project(task)

add_executable(task src/task.cpp)
set_property(TARGET task PROPERTY CXX_STANDARD 20)
