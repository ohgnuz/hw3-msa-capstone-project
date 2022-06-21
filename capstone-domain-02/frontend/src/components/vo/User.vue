<template>
    <div style="margin: 0 -15px 0 -15px;">
        <v-card-title>
            {{label}}
        </v-card-title>
        <v-card-text v-if="value">
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Id" v-model="value.id"/>
            </div>
            <div v-else>
                Id :  {{value.id }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Password" v-model="value.password"/>
            </div>
            <div v-else>
                Password :  {{value.password }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Name" v-model="value.name"/>
            </div>
            <div v-else>
                Name :  {{value.name }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Email" v-model="value.email"/>
            </div>
            <div v-else>
                Email :  {{value.email }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Address" v-model="value.address"/>
            </div>
            <div v-else>
                Address :  {{value.address }}
            </div>
            <div v-if="editMode" style="margin-top:-20px;">
                <v-text-field label="Phone" v-model="value.phone"/>
            </div>
            <div v-else>
                Phone :  {{value.phone }}
            </div>
        </v-card-text>
    </div>
</template>

<script>
    export default {
        name:"User",
        props: {
            editMode: Boolean,
            value : Object,
            label : String, 
        },
        created(){
            if(!this.value) {
                this.value = {
                    'id': '',
                    'password': '',
                    'name': '',
                    'email': '',
                    'address': '',
                    'phone': '',
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