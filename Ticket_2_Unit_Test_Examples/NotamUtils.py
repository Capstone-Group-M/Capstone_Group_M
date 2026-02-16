from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, List

@dataclass
class Notam:
    id: str
    type: str
    location: str
    urgency: str

def fetch_notams() -> List[Notam]:
    notams: List[Notam] = []
    notams.append(Notam("A1", "Runway Closure", "KOKC", "normal"))
    notams.append(Notam("B2", "Airspace Restriction", "KLAX", "high"))
    return notams

def process_notam(raw_notam: Dict[str, str]) -> Notam:
    notam_id = raw_notam.get("id")
    notam_type = raw_notam.get("type")
    location = raw_notam.get("location")
    urgency = raw_notam.get("urgency", "normal")

    return Notam(notam_id, notam_type, location, urgency)

