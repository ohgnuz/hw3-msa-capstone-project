<template>
    <div style="margin: 0 -15px 0 -15px;">
        <v-card-title>
            {{label}}
        </v-card-title>
        <v-card-text v-if="value">
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Street" v-model="value.street"/>
            </div>
            <div v-else>
                Street :  {{value.street }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="City" v-model="value.city"/>
            </div>
            <div v-else>
                City : {{value.city }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="State" v-model="value.state"/>
            </div>
            <div v-else>
                State :  {{value.state }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Country" v-model="value.country" />
            </div>
            <div v-else>
                Country :  {{value.country }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Zipcode" v-model="value.zipcode" />
            </div>
            <div v-else>
                Zipcode :  {{value.zipcode }}
            </div>
        </v-card-text>
    </div>
</template>

<script>
    export default {
        name:"Address",
        props: {
            editMode: Boolean,
            value : Object,
            label : String,
        },
        created(){
            if(!this.value) {
                this.value = {
                    'street': '',
                    'city': '',
                    'state': '',
                    'country': '',
                    'zipcode': '',
                };
            }
        },
        watch: {
            value(newVal) {
                this.$emit('input', newVal);
            },
        },
    }
</script>

<style scoped>
</style>