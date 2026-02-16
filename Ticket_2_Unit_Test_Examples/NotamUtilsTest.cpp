#include <iostream>
#include <cassert>
#include <unordered_map>

// Include the implementation file directly
#include "NotamUtils.cpp"

int main() {
    auto notams = NotamUtils::fetchNotams();
    assert(notams.size() == 2);
    assert(notams[0].id == "A1");
    assert(notams[1].urgency == "high");

    std::unordered_map<std::string, std::string> raw;
    raw["id"] = "C3";
    raw["type"] = "Runway Maintenance";
    raw["location"] = "KJFK";

    auto processed = NotamUtils::processNotam(raw);
    assert(processed.urgency == "normal");

    std::cout << "Tests passed." << std::endl;
    return 0;
}
