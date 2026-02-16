#include <string>
#include <vector>
#include <unordered_map>

class NotamUtils {
public:
    struct Notam {
        std::string id;
        std::string type;
        std::string location;
        std::string urgency;

        Notam(const std::string& id,
              const std::string& type,
              const std::string& location,
              const std::string& urgency)
            : id(id), type(type), location(location), urgency(urgency) {}
    };

    static std::vector<Notam> fetchNotams() {
        std::vector<Notam> notams;
        notams.emplace_back("A1", "Runway Closure", "KOKC", "normal");
        notams.emplace_back("B2", "Airspace Restriction", "KLAX", "high");
        return notams;
    }

    static Notam processNotam(const std::unordered_map<std::string, std::string>& rawNotam) {
        std::string id = rawNotam.at("id");
        std::string type = rawNotam.at("type");
        std::string location = rawNotam.at("location");

        std::string urgency = "normal";
        auto it = rawNotam.find("urgency");
        if (it != rawNotam.end()) {
            urgency = it->second;
        }

        return Notam(id, type, location, urgency);
    }
};
