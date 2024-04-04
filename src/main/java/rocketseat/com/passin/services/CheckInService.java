package rocketseat.com.passin.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rocketseat.com.passin.domain.attendee.Attendee;
import rocketseat.com.passin.domain.checkin.CheckIn;
import rocketseat.com.passin.domain.checkin.exceptions.CheckInAlreadyExistsException;
import rocketseat.com.passin.repositories.CheckInRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;

    public void registerCheckIn(Attendee attendee){

        this.verifyCheckInExists(attendee.getId());

        CheckIn newCheckIn = new CheckIn();
        newCheckIn.setAttendee(attendee);
        newCheckIn.setCreatedAt(LocalDateTime.now());

        this.checkInRepository.save(newCheckIn);

    }

    public Optional<CheckIn> getCheckIn(String attendeeId){
       return this.checkInRepository.findByAttendeeId(attendeeId);
    }

    private void verifyCheckInExists(String attendeeId) {

       CheckIn isCheckIn = this.getCheckIn(attendeeId).orElseThrow(() -> new CheckInAlreadyExistsException("Attendee already checked in"));


    }
}
