# CampusCon - Ticket Generation & AI Description Features

## 🎫 Automatic Ticket Generation
When users register for a deed, the system automatically generates a digital ticket with QR code.

### Ticket Features:
- **Unique Ticket Code**: Format `D123-U456-ABC789` (DeedId-UserId-RandomString)
- **QR Code**: Contains ticket verification data
- **Storage**: QR codes stored as Base64 strings (configurable to file storage)
- **Automatic Generation**: Triggered on successful deed registration

### API Endpoints:

#### Get User's Tickets
```http
GET /api/tickets/my-tickets
Authorization: Bearer {token}
```

#### Get Ticket by Code
```http
GET /api/tickets/code/{ticketCode}
Authorization: Bearer {token}
```

#### Get All Tickets for a Deed
```http
GET /api/tickets/deed/{deedId}
Authorization: Bearer {token}
```

#### Generate Ticket Manually
```http
POST /api/tickets/generate/{deedId}
Authorization: Bearer {token}
```

## 🤖 AI-Powered Description Generation
Users can generate compelling deed descriptions using Gemini AI.

### AI Features:
- **Smart Prompting**: Enhanced prompts based on deed title and category
- **Professional Output**: 100-300 word descriptions optimized for campus events
- **Error Handling**: Graceful fallbacks for API failures
- **Customizable**: Uses provided API key with secure configuration

### API Endpoint:

#### Generate AI Description
```http
POST /api/ai/generate-description
Authorization: Bearer {token}
Content-Type: application/json

{
  "prompt": "A coding competition for computer science students",
  "deedTitle": "CodeFest 2024",
  "category": "TECHNICAL"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "generatedDescription": "Join CodeFest 2024, an exciting coding competition...",
    "success": true,
    "message": "Description generated successfully"
  }
}
```

## 🔧 Configuration

### Application Properties:
```properties
# QR Code Configuration
app.qr.storage.path=uploads/qr-codes
app.qr.base64.enabled=true

# Gemini AI Configuration  
app.gemini.api.key=AIzaSyCYTi7-YbDYLnUOFONE4630KaPZ1ElgPzc
```

## 📊 Database Schema

### Tickets Table:
```sql
CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deed_id BIGINT NOT NULL,
    ticket_code VARCHAR(255) UNIQUE NOT NULL,
    qr_code_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_deed_id (deed_id),
    INDEX idx_ticket_code (ticket_code),
    UNIQUE KEY unique_user_deed (user_id, deed_id)
);
```

## 🚀 Implementation Details

### Services Created:
- **TicketService**: Manages ticket generation and retrieval
- **QRCodeService**: Handles QR code generation and ticket code creation
- **AIDescriptionService**: Integrates with Gemini AI for description generation

### Controllers Created:
- **TicketController**: REST endpoints for ticket management
- **AIDescriptionController**: REST endpoint for AI description generation

### Integration Points:
- **DeedRegistrationController**: Auto-generates tickets on successful registration
- **Enhanced Error Handling**: Graceful failures with proper logging

## 📱 Frontend Integration

### Example: Generate AI Description
```javascript
// Frontend JavaScript example
async function generateAIDescription(prompt, title, category) {
    const response = await fetch('/api/ai/generate-description', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            prompt: prompt,
            deedTitle: title,
            category: category
        })
    });
    
    const result = await response.json();
    return result.data.generatedDescription;
}
```

### Example: Display User Tickets
```javascript
// Fetch and display user tickets
async function loadUserTickets() {
    const response = await fetch('/api/tickets/my-tickets', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    const result = await response.json();
    result.data.forEach(ticket => {
        console.log(`Ticket: ${ticket.ticketCode}`);
        console.log(`QR Code: ${ticket.qrCodePath}`);
    });
}
```

## 🔍 Key Features Summary:

✅ **Automatic Ticket Generation**: Tickets created on deed registration  
✅ **QR Code Integration**: Each ticket has a unique QR code  
✅ **AI Description Writing**: Gemini AI generates professional descriptions  
✅ **Secure API Integration**: Configurable API keys and error handling  
✅ **RESTful APIs**: Complete CRUD operations for tickets  
✅ **Database Integration**: Proper indexing and constraints  
✅ **Error Handling**: Graceful failures and logging
