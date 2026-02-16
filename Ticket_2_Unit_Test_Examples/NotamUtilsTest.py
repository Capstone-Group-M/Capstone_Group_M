import NotamUtils

def test_fetch_notams():
    notams = NotamUtils.fetch_notams()
    assert len(notams) == 2
    assert notams[0].id == "A1"
    assert notams[1].urgency == "high"

def test_process_notam_default_urgency():
    raw = {
        "id": "C3",
        "type": "Runway Maintenance",
        "location": "KJFK"
    }
    processed = NotamUtils.process_notam(raw)
    assert processed.urgency == "normal"