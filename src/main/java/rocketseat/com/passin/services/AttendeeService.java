package rocketseat.com.passin.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import rocketseat.com.passin.domain.attendee.Attendee;
import rocketseat.com.passin.domain.attendee.exceptions.AttendeeAlreadyExistException;
import rocketseat.com.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import rocketseat.com.passin.domain.checkin.CheckIn;
import rocketseat.com.passin.dto.attendee.AttendeeBadgeResponseDTO;
import rocketseat.com.passin.dto.attendee.AttendeeDetailsDTO;
import rocketseat.com.passin.dto.attendee.AttendeesListResponseDTO;
import rocketseat.com.passin.dto.attendee.AttendeeBadgeDTO;
import rocketseat.com.passin.repositories.AttendeeRepository;
import rocketseat.com.passin.repositories.CheckInRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final CheckInService checkInService;


    public List<Attendee>  getAllAttendeesFromEvents(String eventId){

        return this.attendeeRepository.findByEventId(eventId);
    }

    public AttendeesListResponseDTO getEventsAttendee(String eventId){

        List<Attendee> attendeeList = this.getAllAttendeesFromEvents(eventId);

        List<AttendeeDetailsDTO> attendeeDetailsList = attendeeList.stream().map(attendee -> {
            Optional<CheckIn> checkIn = this.checkInService.getCheckIn(attendee.getId());
            LocalDateTime checkedInAt = checkIn.<LocalDateTime>map(CheckIn::getCreatedAt).orElse(null);
            return new AttendeeDetailsDTO(attendee.getId(), attendee.getName(), attendee.getEmail(), attendee.getCreatedAt(), checkedInAt);
        }).toList();

        return new AttendeesListResponseDTO(attendeeDetailsList);
    }

    public Attendee registerAttendee(Attendee newAttendee){
        this.attendeeRepository.save(newAttendee);

        return newAttendee;
    }

    public void verifyAttendeeSubscription(String eventId, String email){
        Optional<Attendee> isAttendeeRegister = this.attendeeRepository.findByEventIdAndEmail(eventId, email);

        if(isAttendeeRegister.isPresent()) throw new AttendeeAlreadyExistException("attendde is already register");

    }

    public AttendeeBadgeResponseDTO getAttendeeBadge(String attendeeId, UriComponentsBuilder uriComponentsBuilder){
        Attendee attendee = this.getAttendee(attendeeId);

        var uri = uriComponentsBuilder.path("/attendees/{attendeeId}/checkIn").buildAndExpand(attendeeId).toUri().toString();


        AttendeeBadgeDTO attendeeBadgeDTO = new AttendeeBadgeDTO(attendee.getName(), attendee.getEmail(), uri, attendee.getEvent().getId());

        return new AttendeeBadgeResponseDTO(attendeeBadgeDTO);



    }

    public void checkInAttendee(String attendeeId) {
        Attendee attendee = this.getAttendee(attendeeId);
        this.checkInService.registerCheckIn(attendee);

    }

    private Attendee getAttendee(String attendeeId){
        return this.attendeeRepository.findById(attendeeId).orElseThrow(() -> new AttendeeNotFoundException("Attendee not found with id" + attendeeId));
    }
}
