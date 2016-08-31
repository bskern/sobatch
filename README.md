# sobatch

The purpose of the project is to programmatically check for new questions I am interested in and email myself. I was
doing this in IFTTT but wanted to build this out myself so I could customize it a bit more. Still nearly and needs more
work but functional for now.

## To run locally you need src/main/resources/application.conf with the following entries:

```
stackoverflow {
  apiKey = YOUR_KEY_HERE
  pageSize = 15
  uri = "/2.2/questions/"
}

emailNotifications {
  to=YOUR_EMAIL_HERE
  from=FILL_THIS_IN
  smtpHost=FILL_THIS_IN
  smtpPort=FILL_THIS_IN
}

```

## Also, code assumes you have available SMTP server
