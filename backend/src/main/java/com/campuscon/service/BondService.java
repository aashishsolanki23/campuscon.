package com.campuscon.service;

import com.campuscon.model.Bond;
import com.campuscon.model.User;
import com.campuscon.repository.BondRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BondService {
    private final BondRepository bondRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    /**
     * Create a new bond request
     */
    @Transactional
    public Bond createBondRequest(Long requesterId, Long receiverId) {
        // Validate users exist
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        // Check if a bond already exists between these users
        Optional<Bond> existingBond = bondRepository.findBondBetweenUsers(requester, receiver);
        if (existingBond.isPresent()) {
            Bond bond = existingBond.get();
            if (bond.getStatus().equals(Bond.BondStatus.ACCEPTED)) {
                throw new RuntimeException("Users are already bonded");
            } else if (bond.getStatus().equals(Bond.BondStatus.PENDING)) {
                // If there's already a pending request
                if (bond.getRequester().equals(requester)) {
                    throw new RuntimeException("You have already sent a bond request to this user");
                } else {
                    // If the receiver sent a request to the requester, accept it
                    bond.setStatus(Bond.BondStatus.ACCEPTED);
                    Bond savedBond = bondRepository.save(bond);
                    
                    // Notify both users
                    notificationService.sendBondAcceptedNotification(
                            bond.getRequester().getId(),
                            bond.getReceiver().getId()
                    );
                    
                    return savedBond;
                }
            } else if (bond.getStatus().equals(Bond.BondStatus.REJECTED)) {
                // Reset rejected bond to pending
                bond.setStatus(Bond.BondStatus.PENDING);
                bond.setRequester(requester);
                bond.setReceiver(receiver);
                Bond savedBond = bondRepository.save(bond);
                
                // Notify receiver
                notificationService.sendBondRequestNotification(
                        requester.getId(),
                        receiver.getId()
                );
                
                return savedBond;
            }
        }
        
        // Create a new bond request
        Bond newBond = new Bond();
        newBond.setRequester(requester);
        newBond.setReceiver(receiver);
        newBond.setStatus(Bond.BondStatus.PENDING);
        Bond savedBond = bondRepository.save(newBond);
        
        // Send notification about new bond request
        notificationService.sendBondRequestNotification(requesterId, receiverId);
        
        return savedBond;
    }
    
    /**
     * Accept a bond request
     */
    @Transactional
    public Bond acceptBondRequest(Long bondId, Long userId) {
        Bond bond = bondRepository.findById(bondId)
                .orElseThrow(() -> new RuntimeException("Bond request not found"));
        
        // Verify the current user is the receiver of this request
        if (!bond.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Only the bond receiver can accept this request");
        }
        
        // Verify the bond is in PENDING state
        if (!bond.getStatus().equals(Bond.BondStatus.PENDING)) {
            throw new RuntimeException("This bond request is not pending");
        }
        
        // Accept the bond
        bond.setStatus(Bond.BondStatus.ACCEPTED);
        Bond acceptedBond = bondRepository.save(bond);
        
        // Send notification about accepted bond
        notificationService.sendBondAcceptedNotification(bond.getRequester().getId(), userId);
        
        return acceptedBond;
    }
    
    /**
     * Reject a bond request
     */
    @Transactional
    public Bond rejectBondRequest(Long bondId, Long userId) {
        Bond bond = bondRepository.findById(bondId)
                .orElseThrow(() -> new RuntimeException("Bond request not found"));
        
        // Verify the current user is the receiver of this request
        if (!bond.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Only the bond receiver can reject this request");
        }
        
        // Verify the bond is in PENDING state
        if (!bond.getStatus().equals(Bond.BondStatus.PENDING)) {
            throw new RuntimeException("This bond request is not pending");
        }
        
        // Reject the bond
        bond.setStatus(Bond.BondStatus.REJECTED);
        return bondRepository.save(bond);
    }
    
    /**
     * Get all pending bond requests for a user
     */
    public List<Bond> getPendingBondRequests(User user) {
        return bondRepository.findPendingBondRequestsForUser(user);
    }
    
    /**
     * Get all users bonded with a given user
     */
    public List<User> getBondedUsers(User user) {
        return bondRepository.findBondedUsers(user);
    }
    
    /**
     * Count accepted bonds for a user (for profile display)
     */
    public Long countBonds(User user) {
        return bondRepository.countAcceptedBonds(user);
    }
    
    /**
     * Check if two users are bonded
     */
    public boolean areUsersBonded(User userOne, User userTwo) {
        return bondRepository.areUsersBonded(userOne, userTwo);
    }
}
