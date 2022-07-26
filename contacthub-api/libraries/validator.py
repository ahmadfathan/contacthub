from wtforms import validators

def OptionalButNotEmpty(form, field):
    if not field.raw_data:
        raise validators.StopValidation()
